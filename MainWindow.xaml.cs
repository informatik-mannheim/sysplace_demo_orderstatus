using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;
using TangibleTouch;
using Path = System.IO.Path;
using Newtonsoft.Json.Linq;

namespace WpfTouchFrameSample
{
	public partial class MainWindow : Window
	{
		private List<TouchDevice> _capturedTouchDevices = new List<TouchDevice>();

		private Touchcode _currentTouchcode;
		private TouchcodeAPI _touchcodeAPI;
		
		private Canvas _canvas;

		private Boolean _touchcodeWasSent = false;

		public MainWindow()
		{
			InitializeComponent();

			_touchcodeAPI = new TouchcodeAPI();
			_currentTouchcode = Touchcode.None;
			_canvas = CreateTouchcodeVisualization();
			
			Redraw();
		}

		private void Redraw()
		{
			xaml_touchpoints.Text = String.Format("{0} TouchPoints @ {1}", _capturedTouchDevices.Count, _touchcodeAPI.Serialize(GetTouchpoints()));
			xaml_touchcode_value.Text = _currentTouchcode.ToString();
		}
		
		private void resetView() {
			xaml_customer.Text = "[customer]";
			xaml_status.Text = "[status]";
			xaml_message.Text = "[message]";
		}

		void OnTouchDown(object sender, TouchEventArgs e)
		{
			grid.CaptureTouch(e.TouchDevice);

			_capturedTouchDevices.Add(e.TouchDevice);
			_currentTouchcode = _touchcodeAPI.Check(GetTouchpoints());
			Console.WriteLine("DEBUG_TOUCHCODE_OnTouchDown: " + _currentTouchcode.ToTouchcodeString());
			
			Redraw();

			if (_currentTouchcode != Touchcode.None)
			{
				// Get information about customer from http://37.61.204.167:8080/string-store/get?key=bestellstatus
				_touchcodeAPI.GetTouchcodeInformation();

				if (_touchcodeAPI.getHtmlResponse() != null)
				{

					JObject responseObject = JObject.Parse(_touchcodeAPI.getHtmlResponse());

					if (responseObject != null)
					{

						foreach (var prop in responseObject)
						{
							var value = prop.Value.ToString();

							switch (prop.Key.ToString())
							{
								case "customer":
									xaml_customer.Text = value;
									break;
								case "status":
									xaml_status.Text = value;
									break;
								case "message":
									xaml_message.Text = value;
									break;
							}
						}

						if (responseObject.Property("status").Value.ToString() == "100")
						{
							// Post the current touch code
							if (!_currentTouchcode.Equals(Touchcode.None))
							{
								_touchcodeAPI.postTouchcode(_currentTouchcode.ToTouchcodeString());
								_touchcodeWasSent = true;
							}
						}

					}
				}
			}
		}

		void OnTouchMove(object sender, TouchEventArgs e)
		{
			_currentTouchcode = _touchcodeAPI.Check(GetTouchpoints());

            // Post the currenct touch code
            // Cache the touch code. Post only if there is a new touch code.
			//if (_currentTouchcode != touchcodeCache)
           //{
			//touchcodeCache = _currentTouchcode;
           //_touchcodeAPI.postTouchcode(_currentTouchcode.ToTouchcodeString());
           //}

			Redraw();
		}

		void OnTouchUp(object sender, TouchEventArgs e)
		{
			grid.ReleaseTouchCapture(e.TouchDevice);

			_capturedTouchDevices.RemoveAll(td => td == e.TouchDevice);
			_currentTouchcode = _touchcodeAPI.Check(GetTouchpoints());
			//Console.WriteLine("DEBUG_TOUCHCODE_OnTouchUp: " + _currentTouchcode.ToTouchcodeString());
            
			// Post the currenct touch code 
			if (_touchcodeWasSent)
			{
				_touchcodeAPI.postTouchcode(_currentTouchcode.ToTouchcodeString());
				if (_currentTouchcode.Equals(Touchcode.None))
				{
					_touchcodeWasSent = false;
				}
			}

			// Reset view
			resetView();

			Redraw();
		}

		private void OnKeyDown(object sender, KeyEventArgs e)
		{
			if (e.Key.ToString().Equals("S"))
			{
				Flash(150);
				WriteSampleToTempLogFile();
			}
		}

		private void WriteSampleToTempLogFile()
		{
			using (StreamWriter file = new StreamWriter(String.Format(@"{0}/touchcode_log.txt", Path.GetTempPath()), true))
			{
				file.WriteLine(_touchcodeAPI.Serialize(GetTouchpoints()));
			}
		}

		private void Flash(int milliseconds)
		{
			var animation = new DoubleAnimation
			{
				AutoReverse = true,
				From = 1,
				To = 0,
				Duration = new TimeSpan(0, 0, 0, 0, milliseconds)
			};

			Storyboard.SetTargetName(animation, grid.Name);
			Storyboard.SetTargetProperty(animation, new PropertyPath(Shape.OpacityProperty));
			Storyboard flashStoryboard = new Storyboard();
			flashStoryboard.Children.Add(animation);
			flashStoryboard.Begin(grid);
		}

		private Canvas CreateTouchcodeVisualization()
		{
			Canvas canvas = new Canvas();
			
			canvas.Children.Add(GetPointAt(new Point(1750 + 33.3, 100)));
			canvas.Children.Add(GetPointAt(new Point(1750 + 66.6, 100)));
			canvas.Children.Add(GetPointAt(new Point(1750, 100 + 33.3)));
			canvas.Children.Add(GetPointAt(new Point(1750 + 33.3, 100 + 33.3)));
			canvas.Children.Add(GetPointAt(new Point(1750 + 66.6, 100 + 33.3)));
			canvas.Children.Add(GetPointAt(new Point(1750 + 100, 100 + 33.3)));
			canvas.Children.Add(GetPointAt(new Point(1750, 100 + 66.6)));
			canvas.Children.Add(GetPointAt(new Point(1750 + 33.3, 100 + 66.6)));
			canvas.Children.Add(GetPointAt(new Point(1750 + 66.6, 100 + 66.6)));
			canvas.Children.Add(GetPointAt(new Point(1750 + 100, 100 + 66.6)));
			canvas.Children.Add(GetPointAt(new Point(1750 + 33.3, 100 + 100)));
			canvas.Children.Add(GetPointAt(new Point(1750 + 66.6, 100 + 100)));
			canvas.Children.Add(DrawLine(new Point(1750, 100), new Point(1750, 200)));
			canvas.Children.Add(DrawLine(new Point(1750, 200), new Point(1850, 200)));

			return canvas;
		}

		private Polygon DrawLine(Point from, Point to)
		{
			var polygon = new Polygon();
			polygon.Stroke = System.Windows.Media.Brushes.Black;
			polygon.Fill = System.Windows.Media.Brushes.LightSeaGreen;
			polygon.StrokeThickness = 2;
			polygon.HorizontalAlignment = HorizontalAlignment.Left;
			polygon.VerticalAlignment = VerticalAlignment.Center;
			polygon.Points.Add(from);
			polygon.Points.Add(to);
			return polygon;
		}

		private Polygon GetPointAt(Point point)
		{
			var polygon = new Polygon();
			polygon.Visibility = Visibility.Hidden;
			polygon.Stroke = System.Windows.Media.Brushes.Crimson;
			polygon.Fill = System.Windows.Media.Brushes.Red;
			polygon.StrokeThickness = 5;
			polygon.HorizontalAlignment = HorizontalAlignment.Left;
			polygon.VerticalAlignment = VerticalAlignment.Center;
			polygon.Points.Add(point);
			polygon.Points.Add(point);
			return polygon;
		}

		private List<TouchPoint> GetTouchpoints()
		{
			return _capturedTouchDevices.Select(td => td.GetTouchPoint(grid)).ToList();
		}
	}
}