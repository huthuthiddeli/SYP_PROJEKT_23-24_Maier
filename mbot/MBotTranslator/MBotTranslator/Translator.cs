using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace MBotTranslator
{
    internal class Translator
    {
        public string Translate(double x, //Left or Right
            double y) //Straight or Back
        {
            int div = (int)Math.Round((100 * y) * x);
            int left;
            int right;

            if (x > 0)
            {
                if (y < 0)
                {
                    right = (int)Math.Round((100*y) + Math.Abs(div));
                    left = (int)Math.Round(100 * y);
                }
                else
                {
                    right = (int)Math.Round((100*y) - Math.Abs(div));
                    left = (int)Math.Round(100 * y);
                }
            }
            else
            {
                if (y < 0)
                {
                    left = (int)Math.Round((100 * y) + Math.Abs(div));
                    right = (int)Math.Round(100 * y);
                }
                else
                {
                    left = (int)Math.Round((100 * y) - Math.Abs(div));
                    right = (int)Math.Round(100 * y);
                }
            }

            return $"{left};{right}";
        }
    }
}
