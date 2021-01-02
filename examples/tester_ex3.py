import glob
import subprocess
import os

subprocess.run('ant')
invalid_files = glob.glob('examples/tests/ex3_tests/invalid/*.xml')
valid_files = glob.glob('examples/tests/ex3_tests/valid/*.xml')
another_invalid_files = glob.glob('examples/tests/ex3_tests/invalid2/*.xml')

invalid_files.sort()
another_invalid_files.sort()
valid_files.sort()
countf =0
countp=0
for file in invalid_files:
    command = f'java -jar mjavac.jar unmarshal semantic {file} out.txt'.split(' ')
    os.remove('out.txt')
    content = ''
    subprocess.run(command)
    with open('out.txt') as out_file:
        content = out_file.read()
    msg = 'faild' if content != 'ERROR\n' else 'success'
    if (msg=='faild'):
    	countf+=1
    else:
    	countp+=1
    print(f'Test {file}: {msg}')
    
for file in another_invalid_files:
    command = f'java -jar mjavac.jar unmarshal semantic {file} out.txt'.split(' ')
    os.remove('out.txt')
    content = ''
    subprocess.run(command)
    with open('out.txt') as out_file:
        content = out_file.read()
    msg = 'faild' if content != 'ERROR\n' else 'success'
    if (msg=='faild'):
    	countf+=1
    else:
    	countp+=1
    print(f'Test {file}: {msg}')

for file in valid_files:
    command = f'java -jar mjavac.jar unmarshal semantic {file} out.txt'.split(' ')
    os.remove('out.txt')
    content = ''
    subprocess.run(command)
    with open('out.txt') as out_file:
        content = out_file.read()
    msg = 'faild' if content != 'OK\n' else 'success'
    if (msg=='faild'):
    	countf+=1
    else:
    	countp+=1
    print(f'Test {file}: {msg}')
    
    
print (f'{countf} Tests failed')
print (f'{countp} Tests passed')