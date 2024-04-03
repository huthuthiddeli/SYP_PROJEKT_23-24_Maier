using Avalonia;
using Avalonia.Controls;
using Avalonia.Controls.Shapes;
using Avalonia.Input;
using Avalonia.Markup.Xaml;
using Avalonia.Media;
using System;

namespace MBotController.Views;

public partial class Joystick : UserControl
{
    private Ellipse thumb;
    private Point startPosition;

    public Joystick()
    {
        this.InitializeComponent();
        this.thumb = this.FindControl<Ellipse>("Thumb");

        this.PointerPressed += OnPointerPressed;
        this.PointerReleased += OnPointerReleased;
        this.PointerMoved += OnPointerMoved;
    }

    private void InitializeComponent()
    {
        AvaloniaXamlLoader.Load(this);
    }

    private void OnPointerPressed(object sender, PointerPressedEventArgs e)
    {
        startPosition = e.GetPosition(this);
    }

    private void OnPointerReleased(object sender, PointerReleasedEventArgs e)
    {
        // Reset joystick position
        Canvas.SetLeft(thumb, 85); // Set initial X position
        Canvas.SetTop(thumb, 85); // Set initial Y position
    }

    private void OnPointerMoved(object sender, PointerEventArgs e)
    {
        if (e.GetCurrentPoint(this).Properties.IsLeftButtonPressed)
        {
            Point currentPosition = e.GetPosition(this);
            Vector offset = currentPosition - startPosition;

            // Limit the joystick movement within a circle
            double radius = 75; // Radius of the joystick movement area
            double length = Math.Sqrt(offset.X * offset.X + offset.Y * offset.Y);
            Vector normalizedOffset = length > radius ? new Vector(offset.X / length * radius, offset.Y / length * radius) : offset;

            Canvas.SetLeft(thumb, 85 + normalizedOffset.X); // Update X position
            Canvas.SetTop(thumb, 85 + normalizedOffset.Y); // Update Y position
        }
    }
}