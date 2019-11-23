#!/usr/bin/env python
import requests
import json
# pip3 install requests

api = "http://localhost:8080"

def login(username, password, parameterName, token):
  data = {
    'username': username,
    'password': password
  }
  data[parameterName] = token
  response = requests.post(api + "/login", data)
  print(response)
  print(response.text)

def csrf():
  response = requests.get(api + "/csrf")
  data = response.json()
  print(data['token'])
  print(data['headerName'])
  print(data['parameterName'])
  return data

csrf_info = csrf()
login("admin", "admin", csrf_info["parameterName"], csrf_info["token"])
csrf()