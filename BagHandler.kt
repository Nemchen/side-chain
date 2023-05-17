package com.desperate.gromov_clo_bot.bot.handler

import com.desperate.gromov_clo_bot.bot.State
import com.desperate.gromov_clo_bot.bot.handler.OrderHandler.Companion.GET_ORDER
import com.desperate.gromov_clo_bot.model.Purchase
import com.desperate.gromov_clo_bot.model.User
import com.desperate.gromov_clo_bot.repository.PurchaseRepository
import com.desperate.gromov_clo_bot.repository.UserRepository
import com.desperate.gromov_clo_bot.util.TelegramUtil
import com.desperate.gromov_clo_bot.util.TelegramUtil.Companion.createInlineKeyboardButton
import com.desperate.gromov_clo_bot.util.TelegramUtil.Companion.createKeyboardRow
import com.desperate.gromov_clo_bot.util.TelegramUtil.Companion.createKeyboardWithRows
import com.desperate.gromov_clo_bot.util.TelegramUtil.Companion.createMessageTemplate
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import java.io.Serializable

@Component
class BagHandler(private val userRepository: UserRepository, private val purchaseRepository: PurchaseRepository) : Handler {

    //Храним поддерживаемые команды в виде команд
    companion object {
        const val DO_ORDER = "Собрать заказ"
        const val BACK = "Назад"
    }

    override fun handle(user: User, message: String): List<PartialBotApiMethod<out Serializable>> {
        return if (message == DO_ORDER) {
            startOrder(user)
        } else {
            goBack(user)
        }
    }

    private fun startOrder(user: User) : List<PartialBotApiMethod<out Serializable>>{
        user.botState = State.ORDER
        userRepository.save(user)

        //var rowOfButtons1  = createKeyboardRow(ADD_PRODUCT)
        var rowOfButtons2  = createKeyboardRow(BACK)
        var keyboard = createKeyboardWithRows(mutableListOf(rowOfButtons2))

        return listOf(createMessageTemplate(user)
                .setText("Сейчас мы, с моей помощью, постараемся оформить Ваш заказ.\n" +
                        "Мне нужно будет узнать название, ссылку на товар, размер товара (если есть)\n" +
                        "Давайте начнем с названия товара(скопируйте с сайта, вставьте сюда и отправьте):")
                .setReplyMarkup(keyboard))
    }

    private fun goBack(user: User): List<PartialBotApiMethod<out Serializable>> {
        user.botState = State.MAIN_MENU
        userRepository.save(user)

        var rowOfButtons1  = createKeyboardRow(GET_ORDER, MainMenuHandler.BAG)
        var rowOfButtons2  = createKeyboardRow("Посмотреть скидки", "В наличии")
        var keyboard = createKeyboardWithRows(mutableListOf(rowOfButtons1, rowOfButtons2))

        return listOf(createMessageTemplate(user)
                .setText("${user.name}, выберите интерсующий Вас пункт меню:")
                .setReplyMarkup(keyboard))
    }

    override fun operatedBotState(): State = State.BAG

    override fun operatedCallBackQuery(): List<String> = listOf(DO_ORDER, BACK)
}