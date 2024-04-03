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
using System.Diagnostics;
using Avalonia.Controls;
using System.Net.Http.Headers;

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
                new MBot("192.168.0.1", 20, 2.99, new List<int>(){1,2,3,4 }, 20, new List<int>(){4,3,2,1 }, 55, 22),
                new MBot("192.168.0.2", 10, 3.99, new List<int>(){1,2,3,4 }, 21, new List<int>(){4,3,2,1 }, 56, 23),
                new MBot("192.168.0.3", 30, 5.99, new List<int>(){1,2,3,4 }, 22, new List<int>(){4,3,2,1 }, 57, 24)
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
            string json = client.GetStringAsync($"http://{IP}:8080/api/mbots").Result;
            var res = client.GetFromJsonAsync<List<MBot>>($"http://{IP}:8080/api/mbots");
            var list = res.Result;
            MBots.AddRange(list);

            MBots.ForEach( mbot => mbot.RandomColor() );

            /*try
            {
                string serverIP = IP;
                int serverPort =  Port;

                TcpClient socket = new TcpClient(serverIP, serverPort);
                socket.ReceiveBufferSize = 1024; // Setze die Puffergröße für den Datenempfang

                NetworkStream stream = socket.GetStream();

                // Registriere den Event Handler für das Empfangen von Daten
                byte[] receivedData = new byte[1024];
                stream.BeginRead(receivedData, 0, receivedData.Length, new AsyncCallback(OnDataReceived), new StateObject { Buffer = receivedData, Stream = stream });

                // Sende Daten an den Server
                string message = "Hallo, Server!";
                byte[] data = Encoding.ASCII.GetBytes(message);
                stream.Write(data, 0, data.Length);
            }
            catch (Exception e)
            {

            }*/
        }

        private async void OnDataReceived(IAsyncResult ar)
        {
            StateObject state = (StateObject)ar.AsyncState;
            NetworkStream stream = state.Stream;
            int bytesRead = stream.EndRead(ar);
            string receivedMessage = Encoding.ASCII.GetString(state.Buffer, 0, bytesRead);
            Debug.WriteLine("Daten empfangen: " + receivedMessage);
        }

        private class StateObject
        {
            public byte[] Buffer { get; set; }
            public NetworkStream Stream { get; set; }
        }

        public async void receiveData()
        {

        }

        public static async Task<string> SendCommand(Command command)
        {
            HttpClient client = new HttpClient();

            string json = JsonSerializer.Serialize(command);
            HttpContent content = new StringContent(json, Encoding.UTF8, "application/json");
            var res = await client.PostAsync($"http://{IP}:8080/api/mbot/commandQueue", content);

            return res.Content.ToString();
        }
    }
}
