

with (open('../../../datasets/input.txt', 'r', encoding='utf-8') as infile,
      open('../../../datasets/output.txt', 'r', encoding='utf-8') as infile2,
      open('../../../datasets/input_test.txt', 'w', encoding='utf-8') as outfile,
      open('../../../datasets/output_test.txt', 'w', encoding='utf-8') as outfile2):
    for i, (line1, line2) in enumerate(zip(infile, infile2), 1):
        if len(line1) < 500 and len(line2) < 500:
            outfile.write(line1)
            outfile2.write(line2)
        # else:
            # print(f"Строка {i} удалена, так как содержит более 500 символов")

print("Сортировка завершена. Результаты сохранены в файле 'output.txt'.")