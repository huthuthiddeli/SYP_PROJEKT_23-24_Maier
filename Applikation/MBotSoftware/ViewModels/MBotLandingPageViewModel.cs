using MBotSoftware.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Reactive.Concurrency;
using ReactiveUI;
using System.Net.Http;
using System.Text.Json;

namespace MBotSoftware.ViewModels
{
    internal class MBotLandingPageViewModel : ViewModelBase
    {
        public List<MBot>? MBots { get; set; }

        public MBotLandingPageViewModel() 
        {
            MBots = [];

            RxApp.MainThreadScheduler.Schedule(LoadData);
        }

        private async void LoadData()
        {
            string url = "";

            //TODO: Get Data from Server and check if works
            try
            {
                HttpClient client = new HttpClient();
                string json = await client.GetStringAsync(url);

                JsonSerializerOptions options = new JsonSerializerOptions();
                options.PropertyNameCaseInsensitive = true;

                MBots = JsonSerializer.Deserialize<List<MBot>>(json, options);
            }
            catch (Exception ex)
            {

            }

            MBots.Add(new MBot("192.168.0.0.1", 20));
        }
    }
}
