using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;
using MBotController.Models;

namespace MBotController.ViewModels
{
    /// <summary>
    /// View displaying all MBots and displaying an overview over them.
    /// </summary>
    internal class MBotLandingViewModel : ViewModelBase
    {
        public List<MBot> MBots { get; set; }

        public MBotLandingViewModel(List<MBot> mBots)
        {
            MBots = mBots;
        }
    }
}
