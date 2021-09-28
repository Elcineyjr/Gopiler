package main

import "fmt"

func main() {
	var num int = 42
	var guess int

	fmt.Println("Enter a number between 0 and 100")
	fmt.Scanln(&guess)

	for guess != num {
		if guess > num {
			fmt.Println("Wrong guess! Try a bit lower (0-100)")
		}

		if guess < num {
			fmt.Println("Wrong guess! Try a bit higher (0-100)")
		}

		fmt.Scanln(&guess)
	}

	fmt.Println("Congrats! You found the secret number")
}
