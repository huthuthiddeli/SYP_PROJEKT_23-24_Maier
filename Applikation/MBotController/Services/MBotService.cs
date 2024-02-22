using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Sockets;
using System.Net;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using MBotController.Models;

namespace MBotController.Services
{
    internal class MBotService
    {
        public static MBotService Instance { get; } = new MBotService();
        private object _lock = new object();
        public List<MBot> MBots { get; set; } = new List<MBot>();
        //TODO: Get MBots from server, otherwise use test data for debug purposes

        private MBotService()
        {
            this.SetItems();
        }

        private void SetItems()
        {
            this.MBots = new List<MBot>()
            {
                new MBot("192.168.0.1", 20),
                new MBot("192.168.0.2", 10),
                new MBot("192.168.0.3", 30)
            };

            /*UdpClient udpClient = new UdpClient();
            udpClient.EnableBroadcast = true;

            IPEndPoint serverEP = new IPEndPoint(IPAddress.Any, 3333);
            udpClient.Client.Bind(serverEP);

            IPEndPoint broadcastEP = new IPEndPoint(IPAddress.Parse("10.10.0.1"), 3333);

            while (true)
            {
                string message = "Hello from client";
                byte[] bytes = Encoding.ASCII.GetBytes(message);

                udpClient.Send(bytes, bytes.Length, broadcastEP);

                Console.WriteLine("Broadcast sent: " + message);

                // Receive response from the server
                byte[] responseData = udpClient.Receive(ref serverEP);
                string responseMessage = Encoding.ASCII.GetString(responseData);
                Console.WriteLine("Received response from server: " + responseMessage);

                // Wait before sending another broadcast
                Thread.Sleep(1000);
            }
            */
        }
    }
}
