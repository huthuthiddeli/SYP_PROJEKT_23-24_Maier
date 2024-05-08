﻿using Avalonia.Media;
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
<<<<<<< Updated upstream
        public string IP {  get; set; }
        [JsonIgnore]
        public string Name { get; set; }
=======
        public string IP { get; set; }
>>>>>>> Stashed changes
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
            this.IP = ip;
            this.Velocity = velocity;
            this.Ultrasonic = ultrasonic;
            this.Angles = angles;
            this.Sound = sound;
            this.LightSensors = lightSensors;
            this.Shake = shake;
            this.Light = light;
            this.Type = type;

            this.CalcLightColors();
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

        private void CalcLightColors()
        {
            for (int i = 0; i < LightSensors.Length; i++)
            {
                int sensor = LightSensors[i];
                byte val = Convert.ToByte(sensor * 2.55);
                IBrush color = new SolidColorBrush(Color.FromRgb(val, val, val));

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
