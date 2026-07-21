def cats = [
        [name: "Sir Pounce", mood: "judgmental", talent: "knocking cups off tables"],
        [name: "Mittens", mood: "chaotic", talent: "opening Zoom calls"],
        [name: "Professor Whiskers", mood: "sleepy", talent: "reviewing code by sitting on it"],
        [name: "Beans", mood: "dramatic", talent: "screaming at closed doors"],
        [name: "Noodle", mood: "suspicious", talent: "detecting empty food bowls from two rooms away"]
]

def crimes = [
        "stole a slice of pizza",
        "reformatted the keyboard layout",
        "merged directly to main",
        "deleted the TODO list because it was emotionally heavy",
        "opened 47 browser tabs about birds"
]

def random = new Random()
def cat = cats[random.nextInt(cats.size())]
def crime = crimes[random.nextInt(crimes.size())]

println "🐾 CAT INCIDENT REPORT 🐾"
println ""
println "Name:   ${cat.name}"
println "Mood:   ${cat.mood}"
println "Talent: ${cat.talent}"
println "Crime:  ${crime}"
println ""

switch (cat.mood) {
    case "judgmental":
        println "${cat.name} has reviewed your life choices and found several warnings."
        break
    case "chaotic":
        println "${cat.name} is currently sprinting through the house for legal reasons."
        break
    case "sleepy":
        println "${cat.name} has postponed all responsibilities until after a 19-hour nap."
        break
    case "dramatic":
        println "${cat.name} believes the bottom of the food bowl is a personal insult."
        break
    default:
        println "${cat.name} is watching you like you know what you did."
}

println ""
println "Verdict: innocent, because cat."
