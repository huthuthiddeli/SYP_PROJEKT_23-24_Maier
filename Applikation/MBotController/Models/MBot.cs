using Avalonia.Media;
using System;
using System.Collections.Generic;
using System.Drawing;
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
        public int Velocity { get; set; } = 0;
        public IBrush BackgroundColor { get; private set; }

        public MBot() 
        {
            this.Name = "MBot " + ID;
            ID++;

            Random random = new();
            BackgroundColor = new SolidColorBrush(Avalonia.Media.Color.FromArgb(1, (byte)random.Next(256), (byte)random.Next(256), (byte)random.Next(256)));
        }

        public MBot(string IP, int velocity)
        {
            this.IP = IP;
            this.Name = "MBot " + ID;
            ID++;
            this.Velocity = velocity;

            Random random = new();
            BackgroundColor = new SolidColorBrush(Avalonia.Media.Color.FromArgb(1, (byte)random.Next(256), (byte)random.Next(256), (byte)random.Next(256)));
        }
    }
}
