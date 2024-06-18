using Avalonia;
using Avalonia.Controls;
using Avalonia.Input;
using Avalonia.Markup.Xaml;
using Avalonia.Media;
using MBotController.Models;
using MBotController.Services;
using MBotController.ViewModels;
using System;
using System.Diagnostics;

namespace MBotController.Views;

public partial class MBotLandingView : UserControl
{
    public MBotLandingView()
    {
        InitializeComponent();

        MBotDetailViewModel? model = this.DataContext as MBotDetailViewModel;

        if (model is null)
        {
            this.DataContext = new MBotLandingViewModel(MBotService.Instance.MBots);
        }
    }

    /// <summary>
    /// Changes to the MBotDetailView when an mbot is selected.
    /// </summary>
    /// <param name="sender"></param>
    /// <param name="args"></param>
    private void Border_PointerPressed(object sender, PointerPressedEventArgs args)
    {
        int id = Convert.ToInt32(((Border)sender).Name);

        MBotLandingViewModel model = this.DataContext as MBotLandingViewModel;
        MBot bot = model.MBots.Find(bot1 => bot1.Id == id);
        this.Content = new MBotDetailView(bot);
    }
}