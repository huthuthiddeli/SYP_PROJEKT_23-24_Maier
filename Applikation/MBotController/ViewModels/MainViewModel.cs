using Avalonia.Controls;
using MBotController.Models;
using MBotController.Services;
using System.Collections.Generic;

namespace MBotController.ViewModels;

internal class MainViewModel : ViewModelBase
{
    public MBotLandingViewModel MBots { get; set; }

    public MainViewModel() 
    {
        MBots = new MBotLandingViewModel(MBotService.Instance.MBots);
    }
}
