using Avalonia.Media;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.Json.Serialization;
using System.Threading.Tasks;

namespace MBotController.Models
{
    internal class MBot
    {
        public static int Count { get; set; } = 0;
        public int ID { get; set; }
        public string IP {  get; set; }
        public string Name { get; set; }
        public int Velocity { get; set; } = 0;
        //TODO: Fix Background Color
        [JsonIgnore]
        public IBrush BackgroundColor { get; private set; }

        public MBot() 
        {
            this.Name = "MBot " + Count;
            this.ID = Count;
            Count++;

            Random random = new();
            BackgroundColor = new SolidColorBrush(Color.FromRgb((byte)random.Next(256), (byte)random.Next(256), (byte)random.Next(256)));
        }

        public MBot(string IP, int velocity)
        {
            this.IP = IP;
            this.Name = "MBot " + Count;
            this.ID = Count;
            Count++;
            this.Velocity = velocity;

            Random random = new();
            BackgroundColor = new SolidColorBrush(Color.FromRgb((byte)random.Next(256), (byte)random.Next(256), (byte)random.Next(256)));
        }
    }
}
