using Avalonia;
using Avalonia.Controls;
using Avalonia.Interactivity;
using Avalonia.Markup.Xaml;
using MBotController.Models;
using MBotController.Services;
using MBotController.ViewModels;
using System.Net.Cache;
using System.Net.Http;

namespace MBotController;

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
    }

    public void sendData(object sender, RoutedEventArgs args)
    {
        Command cmd = new Command(4, "Turn right", "10.10.0.69:1234");

        string res = MBotService.sendCommand(cmd).Result;

        string test = "";
    }
}