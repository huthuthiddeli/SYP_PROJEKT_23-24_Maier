using Avalonia.Media;
using Avalonia.Threading;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.Json.Serialization;
using System.Threading.Tasks;
using CommunityToolkit.Mvvm.ComponentModel;
using Avalonia.Controls;

namespace MBotController.Models
{
    /// <summary>
    /// Class representing the MBot.
    /// </summary>
    partial class MBot : ObservableObject
    {
        [JsonIgnore]
        [ObservableProperty]
        private static int _count = 0;

        // Other properties refactored using [ObservableProperty]

        [JsonIgnore]
        [ObservableProperty]
        private int? _id;

        [ObservableProperty]
        private string _ip;

        [JsonIgnore]
        [ObservableProperty]
        private string _name;

        [JsonIgnore]
        [ObservableProperty]
        private int _velocity = 0;

        [ObservableProperty]
        private double _ultrasonic = 0;

        [ObservableProperty]
        private List<int> _angles = new List<int>();

        [ObservableProperty]
        private int _sound = 0;

        [JsonPropertyName("front_light_sensors")]
        [ObservableProperty]
        private int[] _lightSensors = new int[4];

        [JsonIgnore]
        [ObservableProperty]
        private IBrush[] _lightColors = new IBrush[4];

        [ObservableProperty]
        private int _shake = 0;

        [ObservableProperty]
        private int _light = 0;

        [JsonIgnore]
        [ObservableProperty]
        private IBrush _backgroundColor;

        [ObservableProperty]
        private ConnectionType _type;

        public MBot()
        {
            this.Name = "MBot " + Count;
            this.Id = Count;
            Count++;

            //this.CalcLightColors();
        }

        public MBot(string IP, int velocity) : this()
        {
            this.Ip = IP;
            this.Name = "MBot " + Count;
            this.Id = Count;
            Count++;
            this.Velocity = velocity;
        }

        public MBot(string ip, int velocity, double ultrasonic, List<int> angles, int sound, int[] lightSensors, int shake, int light) : this(ip, velocity)
        {
            Ultrasonic = ultrasonic;
            Angles = angles;
            Sound = sound;
            LightSensors = lightSensors;
            Shake = shake;
            Light = light;
        }

        public MBot(string ip, int velocity, double ultrasonic, List<int> angles, int sound, int[] lightSensors, int shake, int light, ConnectionType type) : this()
        {
            this.Ip = ip;
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

        /// <summary>
        /// Generates a light background color.
        /// </summary>
        public void RandomColor()
        {
            int minVal = 128; // Minimum value to ensure light colors
            int maxVal = 256; // Maximum value for RGB components

            Func<byte> getRandomLightByte = () => (byte)Random.Shared.Next(minVal, maxVal);

            if (Dispatcher.UIThread.CheckAccess())
            {
                this.BackgroundColor = new SolidColorBrush(Color.FromRgb(getRandomLightByte(), getRandomLightByte(), getRandomLightByte()));
            }
            else
            {
                Dispatcher.UIThread.InvokeAsync(() =>
                {
                    this.BackgroundColor = new SolidColorBrush(Color.FromRgb(getRandomLightByte(), getRandomLightByte(), getRandomLightByte()));
                });
            }
        }

        /// <summary>
        /// Calculates the light sensor colors from the data received from the server.
        /// </summary>
        /// <returns>Awaitable task.</returns>
        public async Task CalcLightColors()
        {
            for (int i = 0; i < 4; i++)
            {
                int sensor = LightSensors[i];
                byte val = Convert.ToByte(sensor * 2.55);
                IBrush? color = null;
                color = new SolidColorBrush(Color.FromRgb(val, val, val));

                LightColors[i] = color;
            }
        }

        /// <summary>
        /// Copies the values of another mbot into this mbot.
        /// </summary>
        /// <param name="bot">The bot to copy the data from.</param>
        public void Copy(MBot bot)
        {
            this.Velocity = bot.Velocity;
            this.Sound = bot.Sound;
            this.Ultrasonic = bot.Ultrasonic;
            this.Angles = bot.Angles;
            this.LightSensors = bot.LightSensors;
            this.Light = bot.Light;
            this.Shake = bot.Shake;

            this.CalcLightColors().Wait();
        }
    }
}
