using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using MBotController.Models;

namespace MBotController.Services
{
    internal class MBotService
    {
        //TODO: Get MBots from server, otherwise use test data for debug purposes
        public IEnumerable<MBot> GetItems() => new[]
        {
            new MBot("192.168.0.1", 20),
            new MBot("192.168.0.2", 10),
            new MBot("192.168.0.3", 30),
        };
    }
}
