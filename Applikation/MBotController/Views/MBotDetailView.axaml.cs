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
using System.Diagnostics;
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

        MBotService.Instance.Reset += Reset;
    }

    /// <summary>
    /// Changes to the LandingView when program is reset.
    /// </summary>
    /// <param name="s"></param>
    /// <param name="e"></param>
    public void Reset(object s, EventArgs e)
    {
        this.Content = new MBotLandingView();
    }

    /// <summary>
    /// Gets and returns the light color at index <paramref name="i"/>.
    /// </summary>
    /// <param name="i">Index of the light color</param>
    /// <returns>light color at <paramref name="i"/></returns>
    public IBrush LightColorAtIndex(int i)
    {
        return (this.DataContext as MBotDetailViewModel).Bot.LightColors[i];
    }

    /// <summary>
    /// Sends the command to the server.
    /// </summary>
    /// <param name="sender"></param>
    /// <param name="e"></param>
    public void sendCommand(object sender, KeyEventArgs e)
    {
        var context = this.DataContext as MBotDetailViewModel;
        Command cmd;
        if (e.Key == Key.W)
        {
            cmd = new("0;0.5", context.Bot.Ip);
        }
        else if (e.Key == Key.A)
        {
            cmd = new("-0.5;0", context.Bot.Ip);
        }
        else if (e.Key == Key.S)
        {
            cmd = new("0;-0.5", context.Bot.Ip);
        }
        else if (e.Key == Key.D)
        {
            cmd = new("0.5;0", context.Bot.Ip);
        }
        else
        {
            return;
        }

        MBotService.Instance.Command = cmd;
    }

    /// <summary>
    /// Changes page to LandingView
    /// </summary>
    /// <param name="sender"></param>
    /// <param name="e"></param>
    private void HomeBtnClick(object sender, RoutedEventArgs e)
    {
        this.Content = new MBotLandingView();
    }

    /// <summary>
    /// Changed the pointer position
    /// </summary>
    /// <param name="sender"></param>
    /// <param name="e"></param>
    private void Canvas_PointerPressed(object sender, PointerPressedEventArgs e)
    {
        if (Handle.IsPointerOver)
        {
            handleStartPosition = e.GetPosition(Base);
        }
    }

    /// <summary>
    /// Calculates current Pointer position and sends the coordinates to the server.
    /// </summary>
    /// <param name="sender"></param>
    /// <param name="e"></param>
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
            MBotService.Instance.Command = new Command($"{(-normalizedY).ToString(CultureInfo.InvariantCulture)};{(normalizedX).ToString(CultureInfo.InvariantCulture)}", context.Bot.Ip);
        }
    }

    /// <summary>
    /// Called when the pointer is released and sends commmand to the server to stop the mbot.
    /// </summary>
    /// <param name="sender"></param>
    /// <param name="e"></param>
    private void Canvas_PointerReleased(object sender, PointerReleasedEventArgs e)
    {
        handleStartPosition = null;
        Handle.RenderTransform = new TranslateTransform(0, 0);

        var context = this.DataContext as MBotDetailViewModel;
        MBotService.Instance.Command = new Command("0;0", context.Bot.Ip);
    }

    /// <summary>
    /// Switch the autopilot mode.
    /// </summary>
    /// <param name="sender"></param>
    /// <param name="e"></param>
    public void APBtnToggled(object sender, RoutedEventArgs e)
    {
        MBotService.Instance.SwitchAutoPilotMode((bool)apBtn.IsChecked);
    }

    /// <summary>
    /// Switch the suicide prevention mode.
    /// </summary>
    /// <param name="sender"></param>
    /// <param name="e"></param>
    public void SPBtnToggled(object sender, RoutedEventArgs e)
    {
        MBotService.Instance.SwitchSuicidePreventionMode((bool)apBtn.IsChecked);
    }
}