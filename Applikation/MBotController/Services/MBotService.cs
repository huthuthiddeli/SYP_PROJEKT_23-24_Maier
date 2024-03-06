using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Sockets;
using System.Net;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using MBotController.Models;
using System.Net.Http;
using System.Text.Json;
using System.Net.Http.Json;

namespace MBotController.Services
{
    internal class MBotService
    {
        public static MBotService Instance { get; } = new MBotService();
        private static string IP {  get; set; }
        private static int Port { get; set; }
        private object _lock = new object();
        public List<MBot> MBots { get; set; } = new List<MBot>();
        //TODO: Get MBots from server, otherwise use test data for debug purposes

        private MBotService()
        {
            this.SetItems();
        }

        private void SetItems()
        {
            /*this.MBots = new List<MBot>()
            {
                new MBot("192.168.0.1", 20),
                new MBot("192.168.0.2", 10),
                new MBot("192.168.0.3", 30)
            };*/


            UdpClient udpClient = new UdpClient(6543);
            try
            {
                // Sends a message to the host to which you have connected.
                byte[] sendBytes = Encoding.ASCII.GetBytes("ACC");

                udpClient.Send(sendBytes, sendBytes.Length, IPAddress.Broadcast.ToString(), 5595);

                //IPEndPoint object will allow us to read datagrams sent from any source.
                IPEndPoint RemoteIpEndPoint = new IPEndPoint(IPAddress.Any, 5595);

                // Blocks until a message returns on this socket from a remote host.    
                byte[] receiveBytes = udpClient.Receive(ref RemoteIpEndPoint);
                string returnData = Encoding.ASCII.GetString(receiveBytes);

                if (returnData == "ACC")
                {
                    //Success
                    IP = RemoteIpEndPoint.Address.ToString();
                    Port = RemoteIpEndPoint.Port;
                } 
                else
                {
                    //No success
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
            MBots.Add(client.GetFromJsonAsync<MBot>($"http://{IP}:8080/api/test").Result);
            /*string json = client.GetStringAsync("http://10.10.0.67:8080/api/test").Result;

            JsonSerializerOptions options = new JsonSerializerOptions();
            options.PropertyNameCaseInsensitive = true;

            this.MBots.Add(JsonSerializer.Deserialize<MBot>(json, options));*/
        }
    }
}
