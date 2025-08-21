import pygame.midi

pygame.midi.init()
count = pygame.midi.get_count()

for i in range(count):
    interface, name, is_input, is_output, opened = pygame.midi.get_device_info(i)
    input_str = "Input" if is_input else "Output"
    print(f"ID {i}: {name.decode()} ({interface.decode()}) - {input_str}")

pygame.midi.quit()