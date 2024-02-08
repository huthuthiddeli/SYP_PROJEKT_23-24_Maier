using MBotController.Models;
using MBotController.Services;
using System.Collections.Generic;

namespace MBotController.ViewModels;

internal class MainViewModel : ViewModelBase
{
    public MBotLandingViewModel MBots { get; set; }

    public MainViewModel() 
    {
        MBotService service = new();
        MBots = new MBotLandingViewModel(service.GetItems());
    }
}
