""""This program implements a simple 3-player UNO game.
Authors: Nandhini Subramanyan and Ranjani Subramanyan.
Date:19.06.2018.
Notes to self:
1. Bugs in the program to be fixed : when one player plays skip, plustwo or reverse card, the other player searches for
the color of the thrown action cards and it does not search if he/she has same action card which can be played.
2. And, the program seems to end when the deck becomes empty, but because of while True: statement in game(), program
does not actually break.
3. Can be improved to play with a good strategy.
4. Code to handle these issues still - if the whole deck is emptied before any of the player's deck, shuffle the
cards_thrown list and make it the whole deck
5. Also, decide the winner based on empty deck."""

import random


# This class contains functions to initialise the deck of 108 cards,  to shuffle them in random and
# to assign 7 cards to each player
class Card:
    """"UNO game has 4 coloured cards, numbered from 0 to 9.
    It has three power cards in each colour(Skip, Reverse and plus2).
    Init function initialises the whole deck. No input argument is needed and does not return anything."""
    def __init__(self):
        self.colour = ['B', 'G', 'R', 'Y']
        self.all_cards = list(range(0, 13))  # Skip = 10, Reverse = 11, plus2 = 12
        self.wild_cards = ['plusfour', 'wild']
        self.complete_uno_deck = [self.colour[i] + str(self.all_cards[j]) for i in range(0, 4) for j in range(0, 1)] + \
                                 [self.colour[i] + str(self.all_cards[j]) for i in range(0, 4) for j in
                                  range(1, 13)] * 2 + self.wild_cards * 4

    # This function shuffles the card in random. No input is needed and returns a deck of 108 cards of string type.
    def shuffle(self):
        """Returns a deck of 108 cards of string type"""
        random.shuffle(self.complete_uno_deck)
        print(self.complete_uno_deck)
        return self.complete_uno_deck

    # This function assigns 7 cards to each player. No input is needed and returns 1 hand of 7 cards of string type.
    def player_hand(self):
        """Returns one hand of 7 cards of string type"""
        hand = [self.complete_uno_deck.pop(0) for i in range(0, 7)]
        return hand


# This function is to check if open card from the deck is action card or not.
# The input to this function is a card popped out from the deck of string type format and returns a card of string type
def opencard_check(opening):
    """Returns one card of string type which will be the open card
    opening : 1st card in the deck after assigning cards to the players"""
    if opening not in power_cards:
        open_card = opening
    else:
        p = str(deck.pop(0))
        open_card = opencard_check(p)
    return open_card


# This function checks if the player has same color as that of the open card's color.
def color_check(player_color, open_card_color):
    """Returns boolean value , true or false, depending if the player's card color matches with the open card's color
    player_color : color of the player's card of type string
    open_color : color of the open card of type string"""
    if player_color == open_card_color:
        return True


# This function checks if the player has a number card matching with the number of the open card.
def number_check(player_number, open_card_number):
    """Returns boolean value, true or false, depending if the player's card number matches with the open card's number
    player_number : number of the player's card of string type
    open_card_number : number of open card of string type"""
    if player_number == open_card_number:
        return True


# This function is to separate the color and number of the cards that the player has in hand.
def separate_number_color(p_card):
    """Returns color and number of player's card in separate lists.
    p_card : a list of string type containing the player's cards."""
    color = []
    num = []
    for c in p_card:
        color.append(c[0])
        num.append(c[1:])
    return color, num


# This function determines the color that the player has to choose if he has played a wild or a plusfour card.
def wild_cards_deal(player_cards):
    """Returns a color variable of string type.
    player_cards : a list of cards that a player has in hand of string type.
    if the player does not have any color card in his hand other than the wild cards, then one of the four
    colours is chosen in random."""
    color, number = separate_number_color(player_cards)
    count = 0
    for cl in player.colour:
        if color.count(cl) >= count:
            count = color.count(cl)
            to_return_color = cl
    if count == 0:
        to_return_color = random.choice(player.colour)
    return to_return_color


# This function takes the action for a player depending upon the open_card.
def take_action(open_card, cards, number, open_color):
    """Returns the the card thrown out by the player of string type, the player_deck of cards changed after making the
    changes, such as removing the thrown out card or appending the card taken from the deck, of string type and the
    color of the card that is thrown out.
    open_card : the last card thrown out by the previous player of string type, it can be either just the color or
    the whole card depending on what the previous player had thrown.
    cards : a list of string type containing cards of all the players.
    open_color : a color of string type representing the color of the open card.
    This function is written to handle the power cards in a specific way. And if the player does not have any matching
    card, the player can take card from the deck. Matching is checked for that card too and appropriate action is taken
    As mentioned previously, '10' represents skip action, '11' represents reverse action and '12' represents plus two
    action. 'wild' represents change color action."""
    throw = None
    return_color = open_color
    if open_card not in power_cards:
        open_number = open_card[1:]
        player_colors = []
        player_numbers = []
        player_colors, player_numbers = separate_number_color(cards[number])

        for i in range(0, len(player_colors)):
            if color_check(player_colors[i], open_color):
                throw = cards[number][i]
                break
        if throw is None:
            for i in range(0, len(player_numbers)):
                if number_check(player_numbers[i], open_number):
                    throw = cards[number][i]
                    return_color = throw[0]
                    break
        if throw is None:
            for mycard in cards[number]:
                if mycard in player.wild_cards:
                    throw = mycard
                    return_color = wild_cards_deal(cards[number])
                    break
        if throw is None:
            take_card_from_deck = str(deck.pop(0))
            print("Player " + str(number) + "took the card " + str(take_card_from_deck) + "from deck")
            cards[number].append(take_card_from_deck)
            if color_check(take_card_from_deck[0], open_color):
                throw = cards[number][-1]

            if throw is None:
                if number_check(take_card_from_deck[1:], open_number):
                    throw = cards[number][-1]
                    return_color = throw[0]

            if throw is None:
                if take_card_from_deck in player.wild_cards:
                    throw = take_card_from_deck
                    return_color = wild_cards_deal(cards[number])

        if throw is not None:
            cards[number].remove(throw)
    elif open_card == 'plusfour':
        for i in range(0, 4):
            popped_four = str(deck.pop(0))
            cards[number].append(popped_four)
        throw = open_color
    elif '12' in open_card:
        for i in range(0, 2):
            popped_two = str(deck.pop(0))
            cards[number].append(popped_two)
        throw = open_color
    elif '11' in open_card:
        if number == 0:
            cards = [cards[2]] + [cards[1]] + [cards[0]]
        elif number == 1:
            cards = [cards[1]] + [cards[0]] + [cards[2]]
        elif number == 2:
            cards = [cards[0]] + [cards[2]] + [cards[1]]
        throw = open_color
    elif '10' in open_card:
        throw = open_color
    elif open_card == 'wild':
        for i in range(0, len(cards[number])):
            if color_check(cards[number][i], open_color):
                throw = cards[number][i]
                break
        if throw is None:
            take_card_from_deck = str(deck.pop(0))
            print("Player " + str(number) + "took the card " + str(take_card_from_deck) + "from deck")
            cards[number].append(take_card_from_deck)
            if color_check(take_card_from_deck[0], open_color):
                throw = cards[number][-1]
                cards[number].remove(throw)
            elif take_card_from_deck == 'plusfour' or take_card_from_deck == 'wild':
                throw = take_card_from_deck
                return_color = wild_cards_deal(cards[number])
                cards[number].remove(throw)
            else:
                throw = open_color
    print("Player " + str(number) + "threw the card " + str(throw))
    if throw is None:
        throw = open_card
    if return_color is None:
        return_color = throw[0]
    return cards, throw, return_color


# This function is the start of the game for three players.
def game():
    """This function neither takes any argument as input nor returns anything.
    The program runs until any of the deck becomes empty."""
    player_deck = [player_1, player_2, player_3]
    num_list = [0, 1, 2]
    cards_down = []
    s = str(deck.pop(0))
    open_card = opencard_check(s)
    print("open card is " + open_card)
    cards_down.append(open_card)
    while True:
        for player_number in num_list:
            if player_deck[0] and player_deck[1] and player_deck[2] and deck:
                if cards_down[-1] in player.wild_cards:
                    send_color = returned_color
                else:
                    send_color = cards_down[-1][0]

                player_deck, thrown_card, returned_color = take_action(cards_down[-1], player_deck, player_number,
                                                                       send_color)
                print("The returned values after action:")
                print("deck = " + str(player_deck))
                print("thrown card = " + str(thrown_card))
                print("returned color = " + str(returned_color))
                cards_down.append(thrown_card)
            else:
                break


# Action cards that should not be open card is specified in a list
power_cards = ['plusfour', 'wild', 'B10', 'G10', 'R10', 'Y10', 'B11', 'G11', 'R11', 'Y11', 'B12', 'G12', 'R12', 'Y12']
# Object for the class Card()
player = Card()
deck = player.shuffle()
# Three players are assigned 7 cards at the start of the game
player_1 = player.player_hand()
player_2 = player.player_hand()
player_3 = player.player_hand()
print("player1 card = " + str(player_1))
print("player2 card = " + str(player_2))
print("player3 card = " + str(player_3))
game()
