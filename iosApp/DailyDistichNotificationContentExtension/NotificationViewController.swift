import UserNotifications
import SwiftUI

final class DailyDistichNotificationViewController: UIViewController, UNNotificationContentExtension {
    private let stackView: UIStackView = {
        let stack = UIStackView()
        stack.axis = .vertical
        stack.alignment = .trailing
        stack.spacing = 8
        stack.translatesAutoresizingMaskIntoConstraints = false
        return stack
    }()

    private let headerLabel: UILabel = {
        let label = UILabel()
        label.text = "بیت امروز"
        label.font = .boldSystemFont(ofSize: 15)
        label.textColor = UIColor(red: 0.18, green: 0.23, blue: 0.20, alpha: 1)
        label.textAlignment = .right
        return label
    }()

    private let rightLineLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 15)
        label.textColor = UIColor(red: 0.18, green: 0.23, blue: 0.20, alpha: 1)
        label.textAlignment = .right
        label.numberOfLines = 0
        return label
    }()

    private let leftLineLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 15)
        label.textColor = UIColor(red: 0.18, green: 0.23, blue: 0.20, alpha: 1)
        label.textAlignment = .right
        label.numberOfLines = 0
        return label
    }()

    private let poetLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14)
        label.textColor = UIColor(red: 0.36, green: 0.40, blue: 0.37, alpha: 1)
        label.textAlignment = .left
        return label
    }()

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor(red: 0.96, green: 0.94, blue: 0.91, alpha: 1)
        view.layer.cornerRadius = 16

        stackView.addArrangedSubview(headerLabel)
        stackView.addArrangedSubview(rightLineLabel)
        stackView.addArrangedSubview(leftLineLabel)
        stackView.addArrangedSubview(poetLabel)

        view.addSubview(stackView)
        NSLayoutConstraint.activate([
            stackView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 16),
            stackView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -16),
            stackView.topAnchor.constraint(equalTo: view.topAnchor, constant: 16),
            stackView.bottomAnchor.constraint(equalTo: view.bottomAnchor, constant: -16),
        ])
    }

    func didReceive(_ notification: UNNotification) {
        let userInfo = notification.request.content.userInfo
        rightLineLabel.text = userInfo["right_text"] as? String
        leftLineLabel.text = userInfo["left_text"] as? String
        poetLabel.text = userInfo["poet_name"] as? String
    }
}
