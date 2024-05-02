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
using Microsoft.CodeAnalysis.CSharp.Syntax;
using System.Text.Json.Serialization;

namespace MBotController.Services
{
    /// <summary>
    /// Responsible for data traffic with server.
    /// Implemented as singleton.
    /// </summary>
    internal class MBotService
    {
        private static MBotService _instance = new MBotService();
        public static MBotService Instance { get => _instance; }
        private string IP { get; set; }
        private int Port { get; set; }
        public Command? Command { get; set; }
        public MBot? CurrentBot { get; set; }
        private object _lock = new object();
        public List<MBot> MBots { get; set; }
        public TcpClient TcpClient { get; set; }
        public Thread Thread { get; set; }

        private MBotService()
        {
            MBots = new List<MBot>();

            this.SetItems();

            SendCommand();
        }

        /// <summary>
        /// Responsible to reload the data traffic after server shutdown.
        /// </summary>
        public static void Reload()
        {
            Instance.Thread.Abort();
            Instance.TcpClient.Close();

            _instance = new MBotService();
        }

        /// <summary>
        /// Makes a broadcast to the server, establishes a TCP connection with it and receives the MBots.
        /// </summary>
        private void SetItems()
        {
            /*this.MBots = new List<MBot>()
            {
                new MBot("192.168.0.1", 20, 2.99, new List<int>(){1,2,3,4 }, 20, new List<int>(){4,3,2,1 }, 55, 22),
                new MBot("192.168.0.2", 10, 3.99, new List<int>(){1,2,3,4 }, 21, new List<int>(){4,3,2,1 }, 56, 23),
                new MBot("192.168.0.3", 30, 5.99, new List<int>(){1,2,3,4 }, 22, new List<int>(){4,3,2,1 }, 57, 24)
            };*/

            IPAddress ip = GetLocalIP();
            var serverEP = new IPEndPoint(ip, 5595);
            UdpClient udpClient = new UdpClient(serverEP);
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

            JsonSerializerOptions options = new JsonSerializerOptions();
            options.PropertyNameCaseInsensitive = true;
            options.Converters.Add(new JsonStringEnumConverter());

            string json = client.GetStringAsync($"http://{IP}:8080/api/mbots").Result;
            /*var test = JsonSerializer.Deserialize<List<MBot>>(json, options);*/

            var res = client.GetFromJsonAsync<List<MBot>>($"http://{IP}:8080/api/mbots", options);
            var list = res.Result;
            MBots.AddRange(list);

            MBots.ForEach(mbot => mbot.RandomColor());

            TcpClient = new TcpClient();
            TcpClient.Connect(IPAddress.Parse(IP), 5000);

            this.Thread = new Thread(ReceiveData);
            this.Thread.Start();
        }

        /// <summary>
        /// Calculates the current IP Address.
        /// </summary>
        /// <returns>IP Address in local network.</returns>
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
        /// Method responsible for getting data from server (TCP connection).
        /// </summary>
        public async void ReceiveData()
        {
            while (true)
            {
                if (!TcpClient.Connected)
                {
                    //TODO: reload application
                }

                Stream stream = TcpClient.GetStream();
                byte[] data = new byte[256];

                int bytes = await stream.ReadAsync(data, 0, data.Length);
                string json = Encoding.ASCII.GetString(data, 0, bytes);

                JsonSerializerOptions options = new JsonSerializerOptions();
                options.Converters.Add(new JsonStringEnumConverter());
                options.PropertyNameCaseInsensitive = true;
                MBot? updated = JsonSerializer.Deserialize<MBot>(json, options);

                if (updated is null)
                {
                    continue;
                }

                MBot? existing = MBots.Find(m => m.IP == updated.IP);

                if (existing is null)
                {
                    continue;
                }

                //Check if MBot got deleted
                if (updated.Type == ConnectionType.CONNECTION_CLOSED)
                {
                    MBots.Remove(existing);
                }
                else
                {
                    existing.Copy(updated);
                }

                Thread.Sleep(100);
            }
        }

        /// <summary>
        /// Sends the commands to the server.
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
    }
}
