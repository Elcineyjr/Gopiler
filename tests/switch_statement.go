package main

/* A program that determines the season of the year given a day and a month */
func main() {
	var months [12] int
	months[0] = 31
	months[1] = 28
	months[2] = 31
	months[3] = 30
	months[4] = 31
	months[5] = 30
	months[6] = 31
	months[7] = 31
	months[8] = 30
	months[9] = 31
	months[10] = 30
	months[11] = 31
	
	day := 14
	month := 8

    var month_of_the_year string

    // Here’s a basic switch.
    switch month {
    case 1:
        month_of_the_year = "January"
    case 2:
        month_of_the_year = "February"
    case 3:
        month_of_the_year = "March"
    case 4:
        month_of_the_year = "April"
    case 5:
        month_of_the_year = "May"
    case 6:
        month_of_the_year = "June"
    case 7:
        month_of_the_year = "July"
    case 8:
        month_of_the_year = "August"
    case 9:
        month_of_the_year = "September"
    case 10:
        month_of_the_year = "October"
    case 11:
        month_of_the_year = "November"
    case 12:
        month_of_the_year = "December"
    default:

    }

	day_of_the_year := 0
	var season string

	for i:=0; i<month-1; i++ {
		day_of_the_year += months[i]
	}
	day_of_the_year += day

    // Switch without an expression is an alternate way to express if/else logic.
    // Here we also show how the case expressions can be non-constants
	switch {
	case day_of_the_year < 80:
		season = "Summer"
	case day_of_the_year < 172:
		season = "Autumn"
	case day_of_the_year < 263:
		season = "Winter"
	case day_of_the_year >= 263:
		season = "Spring"
	}

}