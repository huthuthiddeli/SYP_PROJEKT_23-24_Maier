using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.Json.Serialization;
using System.Threading.Tasks;

namespace MBotController.Models
{
    internal class Command
    {
        [JsonPropertyName("id")]
        public int Id { get; set; }
        [JsonPropertyName("name")]
        public string Name { get; set; }
        [JsonPropertyName("socket")]
        public string Socket { get; set; }


        public Command(int id, string name, string socket)
        {
            this.Id = id;
            this.Name = name;
            this.Socket = socket;
        }
    }
}
