using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace MBotController.Models
{
    /// <summary>
    /// Represents the current status of the MBot.
    /// </summary>
    internal enum ConnectionType
    {
        CONNECTION_CLOSED,
        MBOT_DATA,
        CONNECTION_ALIVE,
        MBOT_TEST_DATA
    }
}
