package main

/*
 * Demonstrates the possible ways to declare an array and assign values to it
 */
func main() {

    // Here we create an array that will hold exactly 5 ints
	var a [5]int

    // Use this syntax to declare and initialize an array in one line.
    b := [2]string{"Hello", "World"}
    
    // Assign a value to array at the specified index
	a[3] = 10
	var val = 100
    a[4] = val
}