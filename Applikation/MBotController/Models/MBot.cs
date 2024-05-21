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
        [JsonIgnore]
        public int? ID { get; set; }
        public string IP {  get; set; }
        [JsonIgnore]
        public string Name { get; set; }
        [JsonIgnore]
        public int Velocity { get; set; } = 0;
        public double Ultrasonic { get; set; } = 0;
        public List<int> Angles { get; set; } = new List<int>();
        public int Sound { get; set; } = 0;
        [JsonPropertyName("front_light_sensors")]
        public int[] LightSensors { get; set; } = new int[4];
        [JsonIgnore]
        public IBrush[] LightColors { get; set; } = new IBrush[4];
        public int Shake { get; set; } = 0;
        public int Light { get; set; } = 0;
        [JsonIgnore]
        public IBrush BackgroundColor { get; private set; }
        public ConnectionType Type { get; set; }

        public MBot()
        {
            this.Name = "MBot " + Count;
            this.ID = Count;
            Count++;

            this.RandomColor();
            this.CalcLightColors();
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

        public MBot(string ip, int velocity, double ultrasonic, List<int> angles, int sound, int[] lightSensors, int shake, int light) : this(ip, velocity)
        {
            Ultrasonic = ultrasonic;
            Angles = angles;
            Sound = sound;
            LightSensors = lightSensors;
            Shake = shake;
            Light = light;

            this.CalcLightColors();
        }

        public MBot(string ip, int velocity, double ultrasonic, List<int> angles, int sound, int[] lightSensors, int shake, int light, ConnectionType type) : this()
        {
            this.IP = ip;
            this.Velocity = velocity;
            this.Ultrasonic = ultrasonic;
            this.Angles = angles;
            this.Sound = sound;
            this.LightSensors = lightSensors;
            this.Shake = shake;
            this.Light = light;
            this.Type = type;

            this.CalcLightColors().Wait();
        }

        public void RandomColor()
        {
            if (Dispatcher.UIThread.CheckAccess())
            {
                this.BackgroundColor = new SolidColorBrush(Color.FromRgb((byte)Random.Shared.Next(256), (byte)Random.Shared.Next(256), (byte)Random.Shared.Next(256)));
            }
            else
            {
                Dispatcher.UIThread.InvokeAsync(() =>
                {
                    this.BackgroundColor = new SolidColorBrush(Color.FromRgb((byte)Random.Shared.Next(256), (byte)Random.Shared.Next(256), (byte)Random.Shared.Next(256)));
                });
            }
        }

        private async Task CalcLightColors()
        {
            for (int i = 0; i < LightSensors.Length; i++)
            {
                int sensor = LightSensors[i];
                byte val = Convert.ToByte(sensor * 2.55);
                IBrush? color = null;
                    color = new SolidColorBrush(Color.FromRgb(val, val, val));

                LightColors[i] = color;
            }
        }

        public void Copy(MBot bot)
        {
            this.Velocity = bot.Velocity;
            this.Sound = bot.Sound;
            this.Ultrasonic = bot.Ultrasonic;
            this.Angles = bot.Angles;
            this.LightSensors = bot.LightSensors;
            this.Light = bot.Light;
            this.Shake = bot.Shake;

            this.CalcLightColors();
        }
    }
}
