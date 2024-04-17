using Avalonia.Media;
using Avalonia.Threading;
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
        [JsonIgnore]
        public static int Count { get; set; } = 0;
        public int? ID { get; set; }
        public string IP {  get; set; }
        [JsonIgnore]
        public string Name { get; set; }
        public int Velocity { get; set; } = 0;
        public double Ultrasonic { get; set; } = 0;
        public List<int> Angles { get; set; } = new List<int>();
        public int Sound { get; set; } = 0;
        [JsonPropertyName("front_light_sensors")]
        public List<int> LightSensors { get; set;} = new List<int>();
        public int Shake { get; set; } = 0;
        public int Light { get; set; } = 0;
        [JsonIgnore]
        public IBrush BackgroundColor { get; private set; }

        public MBot() 
        {
            this.Name = "MBot " + Count;
            this.ID = Count;
            Count++;

            this.RandomColor();
        }

        public MBot(string IP, int velocity) : this()
        {
            this.IP = IP;
            this.Name = "MBot " + Count;
            this.ID = Count;
            Count++;
            this.Velocity = velocity;

            this.RandomColor();
            //BackgroundColor = new SolidColorBrush(Color.FromRgb((byte)Random.Shared.Next(256), (byte)Random.Shared.Next(256), (byte)Random.Shared.Next(256)));
        }

        public MBot(string ip, int velocity, double ultrasonic, List<int> angles, int sound, List<int> lightSensors, int shake, int light) : this(ip, velocity)
        {
            Ultrasonic = ultrasonic;
            Angles = angles;
            Sound = sound;
            LightSensors = lightSensors;
            Shake = shake;
            Light = light;
        }

        public void RandomColor()
        {
            if (Dispatcher.UIThread.CheckAccess())
            {
                // Wenn ja, direkt das SolidColorBrush-Objekt erstellen
                this.BackgroundColor = new SolidColorBrush(Color.FromRgb((byte)Random.Shared.Next(256), (byte)Random.Shared.Next(256), (byte)Random.Shared.Next(256)));
            }
            else
            {
                // Andernfalls den Dispatcher verwenden, um den Code auf dem UI-Thread auszuführen
                Dispatcher.UIThread.InvokeAsync(() =>
                {
                    this.BackgroundColor = new SolidColorBrush(Color.FromRgb((byte)Random.Shared.Next(256), (byte)Random.Shared.Next(256), (byte)Random.Shared.Next(256)));
                });
            }
        }
    }
}
