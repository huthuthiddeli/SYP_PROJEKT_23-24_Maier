using System.Threading.Tasks.Dataflow;

namespace MBotTranslator
{
    internal class Program
    {
        static void Main(string[] args)
        {
            Translator trans = new Translator();

            Console.WriteLine("Rechtskurve");
            Console.WriteLine(trans.Translate(.5, 1));
            Console.WriteLine();

            Console.WriteLine("Linkskurve");
            Console.WriteLine(trans.Translate(-.5, 1));
            Console.WriteLine();

            Console.WriteLine("Rückwärts rechtskurve");
            Console.WriteLine(trans.Translate(.5, -1));
            Console.WriteLine();

            Console.WriteLine("Rückwärts linkskurve");
            Console.WriteLine(trans.Translate(-.5, -1));
            Console.WriteLine();

            Console.WriteLine(trans.Translate(.1, .9));
            Console.WriteLine();

            Console.WriteLine(trans.Translate(-.3, -.4));
            Console.WriteLine();
        }
    }
}
