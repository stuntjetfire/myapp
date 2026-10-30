import os, sys, shutil, subprocess, time
cwd = os.getcwd()
android_home = os.path.join(cwd, 'Android/Sdk')
os.environ['ANDROID_HOME'] = android_home
android_jar = os.path.join(android_home,
'platforms/android-36/android.jar')
home = '/data/data/com.termux/files/home'
build = home + '/build.cache'
compiled_res = build + '/compiled_res'
java = build + '/java'
classes = build + '/classes'
dex = build + '/dex'
output = compiled_res + '/output.zip'
temporary = build + '/temporary.apk'
unsignd = build + '/unsigned.apk'
mykey = build + '/mykey.jks'
def check_termux():
  if not os.path.isdir(home):
    sys.exit('not on termux')
def clean_build_directory():
  try:
    shutil.rmtree(build)
  except Exception:
    pass
  if os.path.isdir(build):
    sys.exit('cannot clean previous build')
  try:
    os.mkdir(build)
    os.mkdir(compiled_res)
    os.mkdir(java)
    os.mkdir(classes)
    os.mkdir(dex)
  except Exception:
    print('cannot create build directories')
    raise
def install_packages():
  subprocess.run(['pkg', 'upgrade', '-y'], check=True)
  subprocess.run(['pkg', 'install', '-y',
'--no-install-recommends',
'openjdk-21', 'aapt2', 'zip', 'd8',
'aapt', 'apksigner'],
#zipalign is provided by aapt1
check=True)
def check_packages():
  for x in ['javac', 'aapt2', 'zip', 'd8',
'zipalign', 'apksigner']:
    if shutil.which(x) is None:
      return False
  return True
def compile_java():
  cmd = [
'javac',
'-d',classes,
'-classpath',android_jar
]
  for root, _, files in os.walk('java'):
    for fname in files:
      if fname.lower().endswith('.java'):
        cmd.append(os.path.join(root, fname))
  for root, _, files in os.walk(java):
    for fname in files:
      if fname.lower().endswith('.java'):
        cmd.append(os.path.join(root, fname))
  subprocess.run(cmd,check=True)
def compile_res():
  cmd = [
'aapt2','compile',
'--dir','res',
'-o',output
]
  subprocess.run(cmd,check=True)
def link():
  cmd = [
'aapt2','link',
'--min-sdk-version','21',
'--target-sdk-version','36',
'--version-code','1',
'--version-name','testing',
'-I',android_jar,
'--manifest','AndroidManifest.xml',
'--java',java,
'-o',temporary,
output
]
  subprocess.run(cmd,check=True)
def d8():
  cmd = [
'd8',
'--lib',android_jar,
'--output',dex,
]
  for root, _, files in os.walk(classes):
    for fname in files:
      if fname.lower().endswith('.class'):
        cmd.append(os.path.join(root, fname))
  subprocess.run(cmd,check=True)
def pack():
  cmd = [
'zip',
'-j',temporary,
dex + '/classes.dex'
]
  subprocess.run(cmd,check=True)
  if os.path.isdir('plugins'):
    os.chdir('plugins')
    d = os.listdir('.')
    if d:
      cmd = [
'zip',
'-r',temporary
      ] + d
      subprocess.run(cmd,check=True)
    os.chdir('..')
def zipalign():
  cmd = [
'zipalign',
'-p','4',
temporary,unsignd
  ]
  subprocess.run(cmd,check=True)
def genkey():
  cmd = [
'keytool',
'-genkeypair',
'-keystore',mykey,
'-keyalg','RSA',
'-keysize','2048',
'-validity','10000',
'-alias','ka',
'-storepass','kpasswd'
]
  subprocess.run(cmd,check=True,
input=b'Anonymouse\n\n\n\n\n\nyes\n')
def sign():
  cmd = [
'apksigner','sign',
'--v1-signing-enabled',
'--v2-signing-enabled',
'--v3-signing-enabled',
'--v4-signing-enabled',
'--ks',mykey,
'--ks-key-alias','ka',
'--ks-pass','pass:kpasswd',
'--in',unsignd,
'--out',os.path.basename(cwd) +
time.strftime("_%Y%m%d-%H%M%S.apk")
]
  subprocess.run(cmd,check=True)
def main():
  check_termux()
  clean_build_directory()
  if not check_packages():
    install_packages()
  if not check_packages():
    sys.exit('you must confirm install to proceed')
  compile_res()
  link()
  compile_java()
  d8()
  pack()
  zipalign()
  genkey()
  sign()
  print('all done!')
if __name__ == '__main__':
  main()
