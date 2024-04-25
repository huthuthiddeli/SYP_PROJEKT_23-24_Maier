using Avalonia;
using Avalonia.Controls;
using Avalonia.Controls.Shapes;
using Avalonia.Input;
using Avalonia.Interactivity;
using Avalonia.Markup.Xaml;
using Avalonia.Media;
using MBotController.Models;
using MBotController.Services;
using MBotController.ViewModels;
using System;
using System.Globalization;
using System.Net.Cache;
using System.Net.Http;

namespace MBotController.Views;

internal partial class MBotDetailView : UserControl
{
    private Point? handleStartPosition;

    public MBotDetailView(MBot bot)
    {
        InitializeComponent();

        MBotDetailViewModel? model = this.DataContext as MBotDetailViewModel;

        if (model is null)
        {
            this.DataContext = new MBotDetailViewModel(bot);
        }
        else
        {
            model.Bot = bot;
        }

        Focusable = true;

        AvaloniaXamlLoader.Load(this);
        Base = this.FindControl<Ellipse>("Base");
        Handle = this.FindControl<Ellipse>("Handle");

        Handle.PointerPressed += Canvas_PointerPressed;
        Handle.PointerReleased += Canvas_PointerReleased;
        Handle.PointerMoved += Canvas_PointerMoved;

    }

    public void sendCommand(object sender, KeyEventArgs e)
    {
        var context = this.DataContext as MBotDetailViewModel;
        Command cmd;
        if (e.Key == Key.W)
        {
            cmd = new("0;1", "/" + context.Bot.IP);
        }
        else if (e.Key == Key.A)
        {
            cmd = new("-1;0", "/" + context.Bot.IP);
        }
        else if (e.Key == Key.S)
        {
            cmd = new("0;-1", "/" + context.Bot.IP);
        }
        else if (e.Key == Key.D)
        {
            cmd = new("1;0", "/" + context.Bot.IP);
        }
        else
        {
            return;
        }

        MBotService.Command = cmd;
    }

    private void Canvas_PointerPressed(object sender, PointerPressedEventArgs e)
    {
        if (Handle.IsPointerOver)
        {
            handleStartPosition = e.GetPosition(Base);
        }
    }

    private void Canvas_PointerMoved(object sender, PointerEventArgs e)
    {
        if (handleStartPosition.HasValue)
        {
            var currentPosition = e.GetPosition(Base);
            double deltaX = currentPosition.X - handleStartPosition.Value.X;
            double deltaY = currentPosition.Y - handleStartPosition.Value.Y;

            double distance = Base.Width / 2 - Handle.Width / 2;
            double angle = Math.Atan2(deltaY, deltaX);
            double distanceClamped = Math.Min(Math.Sqrt(deltaX * deltaX + deltaY * deltaY), distance);

            // Calculate translation for the handle
            double translateX = distanceClamped * Math.Cos(angle);
            double translateY = distanceClamped * Math.Sin(angle);

            // Set the translation for the handle
            Handle.RenderTransform = new TranslateTransform(translateX, translateY);

            // Normalize the joystick position to a range of -1 to 1 for both X and Y axes
            double normalizedX = translateX / distance;
            double normalizedY = translateY / distance;

            normalizedX = Math.Round(normalizedX, 2);
            normalizedY = Math.Round(normalizedY, 2);

            var context = this.DataContext as MBotDetailViewModel;
            MBotService.Command = new Command($"{(-normalizedY).ToString(CultureInfo.InvariantCulture)};{(normalizedX).ToString(CultureInfo.InvariantCulture)}", context.Bot.IP);
            Console.WriteLine();
        }
    }

    private void Canvas_PointerReleased(object sender, PointerReleasedEventArgs e)
    {
        handleStartPosition = null;
        Handle.RenderTransform = new TranslateTransform(0, 0);

        var context = this.DataContext as MBotDetailViewModel;
        MBotService.Command = new Command("0;0", context.Bot.IP);
    }
}