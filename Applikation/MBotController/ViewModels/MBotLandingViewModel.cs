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
    internal class MBotLandingViewModel : ViewModelBase
    {
        public List<MBot> MBots { get; set; }

        public MBotLandingViewModel(IEnumerable<MBot> mBots) 
        { 
            using (HttpClient client = new HttpClient())
            {
                MBots = mBots.ToList();
            }
        }

    }
}
