using MBotController.Models;
using MBotController.Services;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace MBotController.ViewModels
{
    internal class MBotDetailViewModel : ViewModelBase
    {
        public MBot Bot { get; set; }

        public MBotDetailViewModel(MBot bot)
        {
            this.Bot = bot;
            MBotService.CurrentBot = bot;
        }
    }
}
