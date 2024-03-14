using Avalonia;
using Avalonia.Controls;
using Avalonia.Markup.Xaml;
using MBotController.Models;
using MBotController.ViewModels;

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
}