
from pathlib import Path


def read_txt(filepath):
    with open(filepath, "r", encoding="utf-8") as file:
        lines = file.readlines()
        return lines


def splitter(text):
    res = []
    start = 0
    i = 0
    while i < len(text):
        curr = text[i]
        if curr == ' ':
            if start != i:
                res.append(text[start:i])
            start = i + 1
        elif curr == ',':
            if start != i:
                res.append(text[start:i])
            res.append(",")
            start = i + 1
        elif curr == '(':
            if text[i + 1] != ')':
                if start != i:
                    res.append(text[start:i])
                res.append("(")
                start = i + 1
        elif curr == ')':
            if text[i - 1] != '(':
                if start != i:
                    res.append(text[start:i])
                res.append(")")
                start = i + 1
        elif curr == ';':
            if start != i:
                res.append(text[start:i])
            res.append(";")
            start = i + 1
        elif curr == '"':
            i += 1
            while text[i] != '"' and i < len(text) - 1:
                i += 1
            start = i + 1
        i += 1

    if start != i:
        res.append(text[start:i])

    return res


input_data = read_txt("../../../datasets/input10.txt")
output_data = read_txt("../../../datasets/output10.txt")

input = [splitter(string) for string in input_data]
output = [splitter(string) for string in output_data]

a = list(Path().glob("c*"))
print(a[-1])

# input_tokenizer = Tokenizer()
# input_tokenizer.fit_on_texts(input)
# print(input_tokenizer[:10])
#
# target_tokenizer = Tokenizer()
# target_tokenizer.fit_on_texts(output)

