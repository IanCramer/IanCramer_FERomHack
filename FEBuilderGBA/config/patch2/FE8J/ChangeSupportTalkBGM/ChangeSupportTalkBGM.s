@Call 08086A70  @ FE8J
@r0   Support Talk Struct
@r4   Support Index 1=?x??C 2=?x??B 3=?x??A
.thumb
cmp r4, #0x3
beq SupportA

cmp r4, #0x2
beq SupportB

SupportC:
ldrh r0, [r0, #0xA]
b    Exit

SupportB:
ldrh r0, [r0, #0xc]
b    Exit

SupportA:
ldrh r0, [r0, #0xE]

Exit:
ldr  r1,=0x08086ABC+1
bx   r1
