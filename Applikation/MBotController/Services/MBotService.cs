﻿using System;
using System.Collections.Generic;
using System.Net.Sockets;
using System.Net;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using MBotController.Models;
using System.Net.Http;
using System.Text.Json;
using System.Net.Http.Json;
using System.IO;
using System.ComponentModel;
using System.Text.Json.Serialization;
using System.Diagnostics;
using Avalonia.Threading;

namespace MBotController.Services
{
    internal class MBotService
    {
        public static MBotService Instance { get; } = new MBotService();
        private string IP { get; set; }
        private int Port { get; set; }
        public Command? Command { get; set; }
        public MBot? CurrentBot { get; set; }
        private object _lock = new object();
        public List<MBot> MBots { get; set; }
        public TcpClient TcpClient { get; set; }
        public Thread Thread { get; set; }
        //TODO: Get MBots from server, otherwise use test data for debug purposes

        private MBotService()
        {
            MBots = new List<MBot>();

            this.SetItems();

            SendCommand();
        }
        private void SetItems()
        {

            /*MBots = new List<MBot>()
            {
                new MBot("192.168.0.1", 20, 2.99, new List<int>(){100,75,50,25 }, 20, [100,75,50,25], 55, 22, ConnectionType.MBOT_TEST_DATA),
                new MBot("192.168.0.2", 10, 3.99, new List<int>(){100,75,50,25 }, 21, [100,75,50,25], 56, 23, ConnectionType.MBOT_TEST_DATA),
                new MBot("192.168.0.3", 30, 5.99, new List<int>(){100,75,50,25 }, 22, [100,75,50,25], 57, 24, ConnectionType.MBOT_TEST_DATA)
            };*/


            IPAddress ip = GetLocalIP();
            var serverEP = new IPEndPoint(ip, 5595);
            UdpClient udpClient = new UdpClient();
            udpClient.EnableBroadcast = true;
            try
            {
                while (true)
                {
                    // Sends a message to the host to which you have connected.
                    byte[] sendBytes = Encoding.UTF8.GetBytes("ACC");

                    udpClient.Send(sendBytes, sendBytes.Length, IPAddress.Broadcast.ToString(), 5595);

                    //IPEndPoint object will allow us to read datagrams sent from any source.
                    IPEndPoint RemoteIpEndPoint = null;// new IPEndPoint(IPAddress.Any, 5595);

                    // Blocks until a message returns on this socket from a remote host.    
                    byte[] receiveBytes = udpClient.Receive(ref RemoteIpEndPoint);
                    string returnData = Encoding.ASCII.GetString(receiveBytes);

                    if (returnData == "ACCACK")
                    {
                        //Success
                        IP = RemoteIpEndPoint.Address.ToString();
                        Port = RemoteIpEndPoint.Port;
                        break;
                    }
                    else
                    {
                        //No success
                    }

                    Thread.Sleep(1000);
                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e.ToString());
            }
            finally
            {
                udpClient.Close();
            }

            HttpClient client = new HttpClient();
            string json = client.GetStringAsync($"http://{IP}:8080/api/mbots").Result;
            JsonSerializerOptions options = new JsonSerializerOptions();
            options.PropertyNameCaseInsensitive = true;
            options.Converters.Add(new JsonStringEnumConverter());

            var res = client.GetFromJsonAsync<List<MBot>>($"http://{IP}:8080/api/mbots", options);
            var list = res.Result;
            MBots.AddRange(list);

            foreach(MBot mbot in list)
            {
                mbot.CalcLightColors().Wait();
                mbot.RandomColor();
            }

            TcpClient = new TcpClient();
            TcpClient.Connect(IPAddress.Parse(IP), 5000);

            //this.Thread = new Thread(ReceiveData);
            //this.Thread.Start();
        }

        public static IPAddress? GetLocalIP()
        {
            var host = Dns.GetHostEntry(Dns.GetHostName());
            foreach (var ip in host.AddressList)
            {
                if (ip.AddressFamily == AddressFamily.InterNetwork)
                {
                    return ip;
                }
            }

            return null;
        }

        public async void ReceiveData()
        {
            while (true)
            {
                Stream stream = TcpClient.GetStream();
                // Buffer to store the response bytes.
                byte[] data = new byte[256];

                // String to store the response ASCII representation.
                string json = string.Empty;

                // Read the first batch of the TcpServer response bytes.
                int bytes = stream.Read(data, 0, data.Length);
                json = Encoding.ASCII.GetString(data, 0, bytes);

                JsonSerializerOptions options = new JsonSerializerOptions();
                options.PropertyNameCaseInsensitive = true;
                options.Converters.Add(new JsonStringEnumConverter());
                MBot? bot = JsonSerializer.Deserialize<MBot>(json, options);
                Debug.WriteLine(bot.IP);

                if (bot is not null)
                {
                    MBot? curr = MBots.Find(m => m.IP == "/" + bot.IP);

                    if (curr is not null)
                    {
                        curr.Copy(bot);
                    }
                }

                Thread.Sleep(100);
            }
        }

        public async void SendCommand()
        {
            HttpClient client = new HttpClient();
            if (CurrentBot is null)
            {
                Command = null;
            }
            else
            {
                Command = new Command("0;0", "/" + CurrentBot);
            }

            while (true)
            {
                if (Command is not null)
                {
                    if (Command.Name[Command.Name.Length - 1] != '!')
                    {
                        Command.Name += "!";
                    }

                    string json = JsonSerializer.Serialize(Command);
                    HttpContent content = new StringContent(json, Encoding.UTF8, "application/json");
                    var res = await client.PostAsync($"http://{IP}:8080/api/mbot/commandQueue", content);
                }

                if (Command is not null && Command.Name == "0;0!")
                {
                    Command = null;
                }

                await Task.Delay(333);
            }
        }
    }
}
