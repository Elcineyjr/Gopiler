package main

import (
    "bufio"
    "fmt"
    "os"
)

func main() {
    reader := bufio.NewReader(os.Stdin)
	fmt.Print("Enter text: ")

	// Nosso programa não sabe lidar com multipla declaração de variaveis simultaneamente
	text := reader.ReadString('\n')
	fmt.Println(text)
}