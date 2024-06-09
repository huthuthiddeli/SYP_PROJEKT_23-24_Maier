﻿using MBotController.Models;
using MBotController.Services;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace MBotController.ViewModels
{
    /// <summary>
    /// View showing all relevant MBot data and controlls.
    /// </summary>
    internal class MBotDetailViewModel : ViewModelBase
    {
        public MBot Bot { get; set; }

        public MBotDetailViewModel(MBot bot)
        {
            this.Bot = bot;
            MBotService.Instance.CurrentBot = bot;
        }
    }
}
