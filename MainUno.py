import client


def start_game():
    host = input('Enter host ip: ')
    player_name = input('Enter you username: ')

    print(host, player_name)
    client.login_to_game(host, player_name)




if __name__ == '__main__':
    start_game()
