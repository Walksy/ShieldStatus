<h1>Shield Statuses!</h1>

This mod renders color overlays on player's shields to determine whether that shield is disabled or not.

---
### Information:
**Colors**

This mod allows you to customize and choose the shield's disabled and enabled color via the mod's config screen. You must have mod menu to access this.


The interpolation option gives you the choice to set the shield to either display two static colors or to have the disabled color gradually transition to the enabled color. This way, you can estimate when the shield will be reactivated. This can be seen in the two videos below:

**Interpolation Enabled:**

![https://i.imghippo.com/files/7LBqE1722036334.gif](https://cdn.modrinth.com/data/GfoMZ9Ly/images/cc69cc08e3fb96bb9b7624303893790a11a1f867.gif)

**Interpolation Disabled:**

![https://i.imghippo.com/files/T4Wbr1722036757.gif](https://cdn.modrinth.com/data/GfoMZ9Ly/images/bc17b904cf4562e19bf4480020000c0bfa6a882b.gif)

**Banner Texture**

![banner](https://www.image2url.com/r2/default/images/1791163919245-a17b3daa-9b42-4cee-bac1-7d230bde1d94.gif)

Put any image (PNG, JPG or animated GIF) on the banner area of every shield.  The rim, handle and back of the shield still show the status colors.

---

## Server Opt-Out

If you don't want your players seeing the shield status of other players, you can switch that part of the mod off. Players will still see the status of their own shield.

Shield Status uses [WalksyLib](https://modrinth.com/mod/walksylib)'s server opt-out, so install the [WalksyLibControl](https://modrinth.com/project/walksylibcontrol) plugin (Spigot, Paper and Folia, 1.20.5+) and run:

```
/wlcontrol disable shieldstatus other-player-shields
or
/wlcontrol disablewithreason shieldstatus other-player-shields "Seeing other players' shield status is not allowed on this server"
```

Only the `other-player-shields` feature can be switched off; disabling `shieldstatus` as a whole has no effect.

---
**Credits**

- Mod: Walksy
- Icon: SakuraFX


