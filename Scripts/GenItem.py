import win32clipboard
import sys

def upcase_first_letter(s):
    return s[0].upper() + s[1:]


data = input(': ')
print(data)
data = data.split(" ")
print(data)


chk = "public boolean Has{0}() {{ return {1} != null; }}"
get = "public {2} Get{0}() {{ return {1}; }}"
set = "public void Set{0}({2} {1}) {{ this.{1} = {1}; }}"

str = chk + "\n" + get + "\n" + set + "\n"
str = str.format(upcase_first_letter(data[1]), data[1], data[0])

win32clipboard.OpenClipboard()
win32clipboard.EmptyClipboard()
win32clipboard.SetClipboardText(str)
win32clipboard.CloseClipboard()
