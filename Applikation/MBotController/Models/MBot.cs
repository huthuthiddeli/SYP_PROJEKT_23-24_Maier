using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace MBotController.Models
{
    internal class MBot
    {
        public static int ID { get; set; } = 0;
        public string IP {  get; set; }
        public string Name { get; set; }
        public int Velocity { get; set; }

        public MBot() 
        {
            this.Name = "MBot " + ID;
        }

        public MBot(string IP)
        {
            this.IP = IP;
            this.Name = "MBot " + ID;
        }
    }
}
