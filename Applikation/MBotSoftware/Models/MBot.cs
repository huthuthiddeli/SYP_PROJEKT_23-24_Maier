using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;

namespace MBotSoftware.Models
{
    internal class MBot
    {
        public static int Count { get; set; } = 0;
        public string Name { get; set; }
        public string Address { get; set; }
        public int Velocity { get; set; }

        public MBot() { }

        public MBot(string address, int velocity)
        {
            this.Name = "MBot" + Count;
            Count++;
            this.Address = address;
            this.Velocity = velocity;
        }
    }
}
