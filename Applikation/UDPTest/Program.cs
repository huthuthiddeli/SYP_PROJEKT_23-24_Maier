using System.Net.Sockets;
using System.Net;
using System.Text;

namespace UDPTest
{
    internal class Program
    {
        static void Main(string[] args)
        {
            UdpClient udpClient = new UdpClient(6543);
            //UdpClient receiver = new UdpClient(6543);
            try
            {
                //udpClient.Connect(IPAddress.Broadcast, 5595);

                // Sends a message to the host to which you have connected.
                byte[] sendBytes = Encoding.ASCII.GetBytes("ACC");

                udpClient.Send(sendBytes, sendBytes.Length, IPAddress.Broadcast.ToString(), 5595);

                //IPEndPoint object will allow us to read datagrams sent from any source.
                IPEndPoint RemoteIpEndPoint = new IPEndPoint(IPAddress.Any, 5595);

                // Blocks until a message returns on this socket from a remote host.    
                byte[] receiveBytes = udpClient.Receive(ref RemoteIpEndPoint);
                string returnData = Encoding.ASCII.GetString(receiveBytes);

                udpClient.Close();

            }
            catch (Exception e)
            {
                Console.WriteLine(e.ToString());
            }
        }
    }
}
