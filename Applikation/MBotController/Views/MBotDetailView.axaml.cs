using Avalonia;
using Avalonia.Controls;
using Avalonia.Input;
using Avalonia.Interactivity;
using Avalonia.Markup.Xaml;
using MBotController.Models;
using MBotController.Services;
using MBotController.ViewModels;
using System.Net.Cache;
using System.Net.Http;

namespace MBotController.Views;

internal partial class MBotDetailView : UserControl
{
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
    }

    public void sendData(object sender, RoutedEventArgs args)
    {
        Command cmd = new Command("Turn right", "10.10.0.69:1234");

        string res = MBotService.SendCommand(cmd).Result;

        string test = "";
    }

    public void sendCommand(object sender, KeyEventArgs e)
    {
        var context = this.DataContext as MBotDetailViewModel;
        Command cmd;
        if (e.Key == Key.W)
        {
            cmd = new("1;0", "/" + context.Bot.IP);
        }
        else if (e.Key == Key.A)
        {
            cmd = new("0;-1", "/" + context.Bot.IP);
        }
        else if (e.Key == Key.S)
        {
            cmd = new("0;1", "/" + context.Bot.IP);
        }
        else if (e.Key == Key.D)
        {
            cmd = new("-1;0", "/" + context.Bot.IP);
        }
        else
        {
            return;
        }

        MBotService.SendCommand(cmd);
    }
}