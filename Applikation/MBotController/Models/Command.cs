using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.Json.Serialization;
using System.Threading.Tasks;

namespace MBotController.Models
{
    /// <summary>
    /// The command which is sent to the mbot.
    /// </summary>
    internal class Command
    {
        [JsonPropertyName("name")]
        public string Name { get; set; }
        [JsonPropertyName("socket")]
        public string Socket { get; set; }


        public Command(string name, string socket)
        {
            this.Name = name;
            this.Socket = socket;
        }

        public override string? ToString()
        {
            return Name + ":" + Socket;
        }
    }
}
