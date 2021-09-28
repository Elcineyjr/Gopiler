package main

import "fmt"

func main() {
	var celsius float32

	fmt.Println("Enter the temperature in °C (Celsius)")
	fmt.Scanln(&celsius)

	var fahrenheit = (celsius * 9.0 / 5.0) + 32.0
	var kelvin = celsius + 273.15

	fmt.Println(celsius, "°C to fahrenheit is", fahrenheit, "°F")
	fmt.Println(celsius, "°C to kelvin is", kelvin, "°K")
}
