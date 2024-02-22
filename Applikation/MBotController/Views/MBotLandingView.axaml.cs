using Avalonia;
using Avalonia.Controls;
using Avalonia.Input;
using Avalonia.Markup.Xaml;
using MBotController.Models;
using MBotController.ViewModels;
using System;
using System.Diagnostics;

namespace MBotController.Views;

public partial class MBotLandingView : UserControl
{
    public MBotLandingView()
    {
        InitializeComponent();
    }

    private void Border_PointerPressed(object sender, PointerPressedEventArgs args)
    {
        int id = Convert.ToInt32(((Border)sender).Name);

        MBotLandingViewModel model = this.DataContext as MBotLandingViewModel;
        MBot bot = model.MBots.Find(bot1 => bot1.ID == id);
        this.Content = new MBotDetailView(bot);
    }
}