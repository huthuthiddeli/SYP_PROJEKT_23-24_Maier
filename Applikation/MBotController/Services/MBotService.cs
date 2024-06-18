using System;
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
    /// <summary>
    /// Service that is responsible with the commmunication to the server.
    /// </summary>
    internal class MBotService
    {
        private static MBotService _instance = new MBotService();
        public static MBotService Instance { get { return _instance; } }
        private string IP { get; set; }
        private int Port { get; set; }
        public Command? Command { get; set; }
        public MBot? CurrentBot { get; set; }
        private object _lock = new object();
        public List<MBot> MBots { get; set; }
        public TcpClient TcpClient { get; set; }
        public Thread Thread { get; set; }
        public event EventHandler Reset;

        private MBotService()
        {
            MBots = new List<MBot>();

            this.SetItems();
        }

        /// <summary>
        /// Makes broadcast and set all the mbots.
        /// </summary>
        private async void SetItems()
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
                    byte[] sendBytes = Encoding.UTF8.GetBytes("ACC");
                    udpClient.Send(sendBytes, sendBytes.Length, IPAddress.Broadcast.ToString(), 5595);

                    IPEndPoint RemoteIpEndPoint = null;
                    byte[] receiveBytes = udpClient.Receive(ref RemoteIpEndPoint);
                    string returnData = Encoding.ASCII.GetString(receiveBytes);

                    if (returnData == "ACCACK")
                    {
                        IP = RemoteIpEndPoint.Address.ToString();
                        Port = RemoteIpEndPoint.Port;
                        break;
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

            foreach (MBot mbot in list)
            {
                mbot.CalcLightColors().Wait();
                mbot.RandomColor();
            }

            SendCommand();

            TcpClient = new TcpClient();
            TcpClient.Connect(IPAddress.Parse(IP), 5000);

            this.Thread = new Thread(ReceiveData);
            this.Thread.Start();
        }

        /// <summary>
        /// Gets the local IP address.
        /// </summary>
        /// <returns>Local IP</returns>
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

        /// <summary>
        /// Responsible for receiving data from the server asynchronously.
        /// Resets the program when the TCP connection from the server is lost.
        /// </summary>
        public async void ReceiveData()
        {
            while (true)
            {
                try
                {
                    Stream stream = TcpClient.GetStream();
                    // Buffer to store the response bytes.
                    byte[] data = new byte[256];

                    // String to store the response ASCII representation.
                    string json = string.Empty;

                    // Read the first batch of the TcpServer response bytes.
                    int bytes = stream.Read(data, 0, data.Length);
                    json = Encoding.UTF8.GetString(data, 0, bytes);

                    JsonSerializerOptions options = new JsonSerializerOptions();
                    options.PropertyNameCaseInsensitive = true;
                    options.Converters.Add(new JsonStringEnumConverter());
                    await Console.Out.WriteLineAsync(json);

                    if (!json.Contains("!"))
                    {
                        continue;
                    }

                    MBot? bot = JsonSerializer.Deserialize<MBot>(json.Split("!")[0], options);

                    if (bot is not null)
                    {
                        MBot? curr = MBots.Find(m => m.Ip == "/" + bot.Ip);

                        if (curr is not null)
                        {
                            curr.Copy(bot);
                        }
                    }

                    Thread.Sleep(100);
                }
                catch (JsonException ex)
                {
                    Console.WriteLine(ex.Message);
                }
                catch (SocketException ex)
                {
                    await Dispatcher.UIThread.InvokeAsync(async () =>
                    {
                        await Console.Out.WriteLineAsync("lost connection");
                        Reset?.Invoke(this, EventArgs.Empty);
                        _instance = new MBotService();
                        return;
                    });
                }
                catch (IOException ex)
                {
                    await Dispatcher.UIThread.InvokeAsync(async () =>
                    {
                        await Console.Out.WriteLineAsync("lost connection");
                        Reset?.Invoke(this, EventArgs.Empty);
                        _instance = new MBotService();
                        return;
                    });
                }
            }
        }

        /// <summary>
        /// Sends commands to the server via the mbot/commandQueue route asynchrounously.
        /// </summary>
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

        /// <summary>
        /// Switches the autopilot mode of the mbot.
        /// </summary>
        /// <param name="turnedOn">Sets the autopilot mode to either on (true) or off (false)</param>
        public async void SwitchAutoPilotMode(bool turnedOn)
        {
            NetworkStream stream = TcpClient.GetStream();

            bool autoPilot = turnedOn;
            byte[] data = JsonSerializer.SerializeToUtf8Bytes(autoPilot);

            await stream.WriteAsync(Encoding.UTF8.GetBytes("autoPilot:" + turnedOn));
        }

        /// <summary>
        /// Switches the suicide prevention mode of the mbot.
        /// </summary>
        /// <param name="turnedOn">Sets the suicide prevention mode to either on (true) or off (false)</param>
        public async void SwitchSuicidePreventionMode(bool turnedOn)
        {
            NetworkStream stream = TcpClient.GetStream();

            bool preventCollision = turnedOn;
            string json = JsonSerializer.Serialize(preventCollision);
            byte[] data = JsonSerializer.SerializeToUtf8Bytes(preventCollision);

            await stream.WriteAsync(Encoding.UTF8.GetBytes("preventCollision:" + turnedOn));
        }
    }
}
